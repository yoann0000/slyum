package dataRecord.elements;

import dataRecord.elementType.ElementType;
import dataRecord.io.ElementVisitor;

public class InterfaceField extends Field
{
	public InterfaceField(String name, ElementType type,String value, int id)
	{
		super(name, type, value, id);
		setFinal(true);
	}
	
	public InterfaceField(String name, ElementType type,String value)
	{
		super(name, type, value);
		setFinal(true);
	}

	@Override
	public String accept(ElementVisitor visitor)
	{
		return visitor.visit(this);
	}

	@Override
	public String toString()
	{
		String tmp = "";
		tmp += type.getElementType();
		tmp += " ";
		tmp += name;
		
		if (!getValue().isEmpty())
			tmp += " =" + getValue();

		return tmp;
	}

}