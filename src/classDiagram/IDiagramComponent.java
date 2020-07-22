package classDiagram;

import java.util.Observer;
import swing.XmlElement;

/**
 * Interface implemented by all class diagram component.
 * 
 * @author David Miserez
 * @version 1.0 - 24.07.2011
 * 
 */
public interface IDiagramComponent extends XmlElement {
  public enum UpdateMessage {
    ADD_ATTRIBUTE, ADD_METHOD, ADD_ATTRIBUTE_NO_EDIT, ADD_METHOD_NO_EDIT, ADD_ENUM, ADD_ENUM_NO_EDIT, MODIF, SELECT,
    UNSELECT, ADD_VIEW, ADD_KEY, ADD_KEY_NO_EDIT, ADD_TRIGGER, ADD_TRIGGER_NO_EDIT
  };

  public void addObserver(Observer o);
  public void deleteObserver(Observer o);
  public int countObservers();

  /**
   * Get the id of the component.
   * 
   * @return the id of the component.
   */
  public int getId();

  public void notifyObservers();

  // all IDiagramComponent must implement an Observer - Observable structure.
  public void notifyObservers(Object arg);

  /**
   * Select the component. This method just setChanged the component, you must
   * notify with the UpdateMessage.SELECT for appling change.
   */
  public void select();
}
