package swing;

import classDiagram.ClassDiagram;
import classDiagram.components.Entity;
import graphic.GraphicComponent;
import graphic.GraphicView;
import graphic.entity.EntityView;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import swing.dialog.DialogDeleteView;
import swing.hierarchicalView.HierarchicalView;
import utility.RelValidation.RelValidator;
import utility.SMessageDialog;
import utility.SQLconverter.SQLconverter;
import utility.Utility;
import utility.WatchDir;
import utility.relConverter.RelConverter;

/**
 * 
 * @author David Miserez <david.miserez@heig-vd.ch>
 */
public class MultiViewManager {
  
  // Static stuff
  private static MultiViewManager instance;
  public static void initialize(
      ClassDiagram classDiagram, HierarchicalView hierarchicalView) {
    instance = new MultiViewManager(classDiagram, hierarchicalView);
  }
  
  public static GraphicView getRootGraphicView() {
    return instance.graphicViews.getFirst();
  }
  
  static GraphicView lastSelectedGraphicView = null;
  public static GraphicView getSelectedGraphicView() {
    try {
      STab.GraphicViewTabComponent component = 
          STab.getInstance().getTabComponentAt(
              STab.getInstance().getSelectedIndex());

      if (component != null)
        return lastSelectedGraphicView = component.getGraphicView();
    } catch (Exception e) {
      return lastSelectedGraphicView;
    }
    return lastSelectedGraphicView;
  }
  
  public static List<GraphicView> getAllGraphicViews() {
    return (List<GraphicView>) instance.graphicViews.clone();
  }
  
  public static List<GraphicView> getAllClosedGraphicViews() {
    LinkedList<GraphicView> list = new LinkedList<>();
    for (GraphicView gv : getAllGraphicViews())
      if (!gv.isOpenInTab())
        list.add(gv);
    return list;
  }
  
  public static boolean isGraphicViewOpened(GraphicView graphicView) {
    return STab.getInstance().getAllGraphicsView().contains(graphicView);
  }    
  
  public static boolean isRootViewSelected() {
    return getSelectedGraphicView() == getRootGraphicView();
  }
  
  public static GraphicView addNewView() {    
    ViewCreationDialog uip =
        new ViewCreationDialog(
            GraphicView.NO_NAMED_VIEW, "Slyum - New view", "Enter a name for the new view:");
    
    uip.setVisible(true);

    if (uip.isAccepted())
      return uip.isRel() ? addNewView(uip.getText() + " - REL", true)
                         : addNewView(uip.getText() + " - UML", false);

    return null;
  }
  
  public static GraphicView addNewView(String title, boolean rel) {
    GraphicView newGraphicView = new GraphicView(instance.classDiagram, false);
    newGraphicView.setRelational(rel);
    instance.graphicViews.add(newGraphicView);
    instance.hierarchicalView.addView(newGraphicView);
    newGraphicView.setName(title);
    newGraphicView.notifyObservers();
    
    if (!isXmlImportation())
      addNewViewInFile(newGraphicView);
    
    return newGraphicView;
  }

  public static GraphicView addNewView(GraphicView newGraphicView) {
    instance.graphicViews.add(newGraphicView);
    instance.hierarchicalView.addView(newGraphicView);
    newGraphicView.notifyObservers();

    return newGraphicView;
  }
  
  public static void renameView(GraphicView graphicView, String newName) {
    if (MultiViewManager.getRootGraphicView() == graphicView) {
      ClassDiagram cd = graphicView.getClassDiagram();
      cd.setName(newName);
      cd.notifyObservers();
      return;
    }
    
    graphicView.setName(newName);
    graphicView.notifyObservers();
  }
  
  public static String getViewName(GraphicView graphicView) {
    if (MultiViewManager.getRootGraphicView() == graphicView)
      return graphicView.getClassDiagram().getName();
    return graphicView.getName();
  }
  
  public static GraphicView openView(GraphicView graphicView) {
    
    if (graphicView == null)
      return null;
    
    if (!graphicView.isOpenInTab()) {
      STab.getInstance().openTab(graphicView);
      
      if (!isXmlImportation())
        changeViewStatInFile(graphicView, true, graphicView.isRelational());
    }
    
    setSelectedGraphicView(graphicView);
    
    return graphicView;
  }
  
  public static void setSelectedGraphicView(int index) {
    setSelectedGraphicView(instance.graphicViews.get(index));
  }
  
  public static void setSelectedGraphicView(GraphicView graphicView) {
    
    if (STab.getInstance().indexOfComponent(graphicView.getScrollPane()) == -1)
      return;
    
    if (STab.getInstance().getSelectedComponent() != graphicView.getScrollPane())
      STab.getInstance().setSelectedComponent(graphicView.getScrollPane());

    instance.hierarchicalView.setSelectedView(graphicView);
    PanelClassDiagram.refreshHierarchicalView();
  }
  
  public static GraphicView closeView(GraphicView graphicView) {
    if (!isXmlImportation())
      changeViewStatInFile(graphicView, false, graphicView.isRelational());
    
    JTabbedPane pane = STab.getInstance();
    int selectedIndex = pane.getSelectedIndex();
    int index = pane.indexOfComponent(graphicView.getScrollPane());
    pane.remove(graphicView.getScrollPane());
    pane.setSelectedIndex(
        selectedIndex == index ? 
            index - (index == pane.getTabCount() - 1 ? 1 : 0) : 
            (selectedIndex > index ? selectedIndex - 1 : selectedIndex));
    return graphicView;
  }
  
  public static GraphicView closeSelectedView() {
    return closeView(getSelectedGraphicView());
  }
  
  public static GraphicView closeSelectedViewWithWarning() {
    if (isRootViewSelected()) {
      SMessageDialog.showErrorMessage("The main view can't be closed.");
      return null;
    }
      
    return closeSelectedView();
  }
  
  public static void removeView(GraphicView graphicView) {
    if (graphicView == getRootGraphicView())
      throw new IllegalArgumentException(
          "You cannot remove the main graphic view. ");
    
    // Ask the user if he realy want to delete the view.
    if (!DialogDeleteView.show(
          graphicView.getName(), 
          getArrayNameComponents(graphicView.getAllUniqueEntities())))
      return;
    
    if (graphicView.isOpenInTab())
      closeView(graphicView);
    
    cleanViewBeforeDelete(graphicView);
    instance.hierarchicalView.removeView(graphicView);
    removeViewInFile(graphicView);
    instance.graphicViews.remove(graphicView);
  }
  
  private static String[] getArrayNameComponents(List<EntityView> components) {
    int size = components.size();
    String[] strs = new String[size];
    for (int i = 0; i < size; ++i)
      strs[i] = ((Entity)components.get(i).getAssociedComponent()).getName();
    return strs;
  }
  
  /**
   * Delete all component's view that are only in this view.
   * @param graphicView The view.
   */
  private static void cleanViewBeforeDelete(GraphicView graphicView) {
    for (GraphicComponent gc : graphicView.getAllGraphicalUniqueComponent())
      gc.delete();
  }
  
  public static void removeSelectedView() {
    removeView(getSelectedGraphicView());
  }
  
  public static GraphicView addAndOpenNewView() {
    return openView(addNewView());
  }

  public static GraphicView addAndOpenNewView(GraphicView graphicView) {
    return openView(addNewView(graphicView));
  }
  
  public static GraphicView addAndOpenNewView(String title, boolean rel) {
    return openView(addNewView(title, rel));
  }
  
  public static void cleanGraphicViews() {
    while (instance.graphicViews.size() > 1) { // Do not remove the root graphic view.
      instance.classDiagram.removeComponentsObserver(instance.graphicViews.get(1));
      instance.graphicViews.remove(1);
    }
  }
  
  public static void changeViewStatInFile(GraphicView graphicView, boolean open, boolean rel) {
    if (getCurrentFile() == null)
      return;
    
    WatchDir.stopWatchingFile(getCurrentPath(), true);
    try {
      
      String strOpen = String.valueOf(open);
      Document doc = getDocumentFromCurrentFile();
      String strRel = String.valueOf(rel);
      
      Node nodeUmlView = doc.getElementsByTagName("umlView").item(
          instance.graphicViews.indexOf(graphicView));

      // Update the open attribute of the umlView node.
      Node openNode = nodeUmlView.getAttributes().getNamedItem("open");
      
      if (openNode == null)
        ((Element)nodeUmlView).setAttribute("open", strOpen);
      else
        openNode.setTextContent(strOpen);

      //Same for relational
      Node relNode = nodeUmlView.getAttributes().getNamedItem("rel");

      if (relNode == null)
        ((Element)nodeUmlView).setAttribute("rel", strOpen);
      else
        relNode.setTextContent(strOpen);

      saveDocumentInCurrentFile(doc);
      
    } catch (TransformerException | ParserConfigurationException | SAXException | IOException ex) {
      Logger.getLogger(PanelClassDiagram.class.getName()).log(Level.SEVERE, null, ex);
    }    
    SwingUtilities.invokeLater(() -> WatchDir.stopWatchingFile(getCurrentPath(), false));
	}
  
  private static Path getCurrentPath() {
    return PanelClassDiagram.getInstance().getCurrentPath();
  }
  
  private static File getCurrentFile() {
    return PanelClassDiagram.getInstance().getCurrentFile();
  }
  
  private static void removeViewInFile(GraphicView graphicView) {
    if (getCurrentFile() == null)
      return;
    try {
      int viewIndex = instance.graphicViews.indexOf(graphicView);
      Document doc = getDocumentFromCurrentFile();
      
      doc.getFirstChild().removeChild(doc.getElementsByTagName("umlView").item(viewIndex));
      saveDocumentInCurrentFile(doc);
    } catch (ParserConfigurationException | SAXException | IOException | TransformerException ex) {
      Logger.getLogger(PanelClassDiagram.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
  private static void addNewViewInFile(GraphicView graphicView) {
    if (getCurrentFile() == null)
      return;
    try {
      Document doc = getDocumentFromCurrentFile();
      
      doc.getFirstChild().appendChild(graphicView.getXmlElement(doc));
      saveDocumentInCurrentFile(doc);
    } catch (ParserConfigurationException | SAXException | IOException | TransformerException ex) {
      Logger.getLogger(PanelClassDiagram.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
  private static Document getDocumentFromCurrentFile() 
      throws ParserConfigurationException, SAXException, IOException {
    String filepath = getCurrentFile().getAbsolutePath();
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docFactory.newDocumentBuilder(); 
    return docBuilder.parse(filepath);
  }
  
  private static void saveDocumentInCurrentFile(Document doc) 
      throws TransformerException {
    PanelClassDiagram.saveDocumentInCurrentFile(doc, PanelClassDiagram.getFileOpen());
  }
  
  private static boolean isXmlImportation() {
    return PanelClassDiagram.getInstance().isXmlImportation();
  }
  
  private ClassDiagram classDiagram;
  private final HierarchicalView hierarchicalView;
  private final LinkedList<GraphicView> graphicViews = new LinkedList<>();

  private MultiViewManager(
      ClassDiagram classDiagram, HierarchicalView hierarchicalView) {
    
    GraphicView rootGraphicView = new GraphicView(classDiagram, true);
    graphicViews.add(rootGraphicView);
    
    this.classDiagram = classDiagram;
    this.hierarchicalView = hierarchicalView;
    
    STab.initialize(rootGraphicView);
  }

  public static void ConvertSelectedView() {
    if (getSelectedGraphicView().isRelational()) //should never happen but just in case
      JOptionPane.showMessageDialog(Slyum.getInstance(), "You must select a UML view to convert");

    else {
      int dialogResult = JOptionPane.showConfirmDialog (Slyum.getInstance(),
              "Convert \"" + getSelectedGraphicView().getName() + "\" to REL"
              ,"Conversion confirmation",JOptionPane.YES_NO_OPTION);
      if(dialogResult == JOptionPane.YES_OPTION){
        RelConverter.getInstance().setGraphicView(getSelectedGraphicView());
        RelConverter.getInstance().umlToRel();
        JOptionPane.showMessageDialog(Slyum.getInstance(), "Default keys have been added.\n " +
                "You must add attributes to them");
      }
    }
  }

  public static void SQLSelectedView() {
    if (getSelectedGraphicView().isRelational()) {
      RelValidator rv = RelValidator.getInstance();
      rv.setComponents(getSelectedGraphicView().getAllDiagramComponents());
      rv.validate();
      if (rv.getErrors() != 0) {
        JOptionPane.showMessageDialog(Slyum.getInstance(), rv.getErrorString(), "Diagram errors", JOptionPane.ERROR_MESSAGE);
        return;
      }

      Object[] possibilities = {"MYSQL", "POSTGRES", "SQ LITE"};
      String s = (String)JOptionPane.showInputDialog(
              Slyum.getInstance(),
              "Select desired Database",
              "SQL conversion",
              JOptionPane.QUESTION_MESSAGE,
              null,
              possibilities,
              "POSTGRES");
      SQLconverter.getInstance().setRelGraphicView(getSelectedGraphicView());
      String SQL = SQLconverter.getInstance().convertToSQL(s);

      if (SQL.equals("unimplemented"))
        JOptionPane.showMessageDialog(Slyum.getInstance(), "This database has yet to be implemented");
      else
        exportAsSQL(SQL);
    }
    else {
      JOptionPane.showMessageDialog(Slyum.getInstance(), "You must select a relational view to convert");
    }
  }


  private static void exportAsSQL(String sql) {
    final JFileChooser fc = new JFileChooser(Slyum.getCurrentDirectoryFileChooser());
    fc.setAcceptAllFileFilterUsed(false);

    fc.addChoosableFileFilter(new FileFilter() {

      @Override
      public boolean accept(File f) {
        if (f.isDirectory()) return true;

        final String extension = Utility.getExtension(f);
        if (extension != null)
          return extension.equals("sql");

        return false;
      }

      @Override
      public String getDescription() {
        return "SQL (*.sql)";
      }
    });

    final int result = fc.showSaveDialog(Slyum.getInstance());

    if (result == JFileChooser.APPROVE_OPTION) {

      File file = fc.getSelectedFile();
      String extension = Utility.getExtension(file);

      if (extension == null)
        file = new File(file.getPath() + ".sql");

      exportFileTo(file, sql);
    }
  }

  private static void exportFileTo(File file, String sql) {
    if (file.exists())
      if (SMessageDialog.showQuestionMessageOkCancel(file + " already exists. Overwrite?") == JOptionPane.CANCEL_OPTION)
        return;

    try {
      FileWriter writer =new FileWriter(file);
      writer.write(sql);
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
