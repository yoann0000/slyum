package classDiagram.verifyName;

import classDiagram.components.Trigger;

public class TriggerName implements IVerifyName {

    private static TriggerName instance;

    public static TriggerName getInstance() {
        if (instance == null) instance = new TriggerName();

        return instance;
    }

    private TriggerName() {}

    @Override
    public boolean verifyName(String name) {
        return !name.isEmpty() && Trigger.checkSemantic(name);
    }

    public static String verifyAndAskNewName(String name)
            throws SyntaxeNameException {
        return ValidationName.checkAndAskName(name, MethodName.getInstance());
    }
}
