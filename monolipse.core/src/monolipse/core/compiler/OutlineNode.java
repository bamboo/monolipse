package monolipse.core.compiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class OutlineNode {
	
	public static final OutlineNode[] NO_CHILDREN = new OutlineNode[0];
	
	public static final String INTERFACE 			= "InterfaceDefinition";
	public static final String PROTECTED_INTERFACE 	= "Protected_InterfaceDefinition";
	public static final String PRIVATE_INTERFACE 	= "Private_InterfaceDefinition";
	public static final String INTERNAL_INTERFACE 	= "Internal_InterfaceDefinition";

	public static final String CLASS 				= "ClassDefinition";
	public static final String PROTECTED_CLASS 		= "Protected_ClassDefinition";
	public static final String PRIVATE_CLASS 		= "Private_ClassDefinition";
	public static final String INTERNAL_CLASS 		= "Internal_ClassDefinition";
	
	public static final String METHOD 				= "Method";
	public static final String PROTECTED_METHOD 	= "Protected_Method";
	public static final String PRIVATE_METHOD 		= "Private_Method";
	public static final String INTERNAL_METHOD 		= "Internal_Method";

	public static final String PROPERTY 			= "Property";
	public static final String PROTECTED_PROPERTY 	= "Protected_Property";
	public static final String PRIVATE_PROPERTY 	= "Private_Property";
	public static final String INTERNAL_PROPERTY 	= "Internal_Property";

	public static final String FIELD 				= "Field";
	public static final String PROTECTED_FIELD 		= "Protected_Field";
	public static final String PRIVATE_FIELD 		= "Private_Field";
	public static final String INTERNAL_FIELD 		= "Internal_Field";

	public static final String ENUM 				= "EnumDefinition";
	public static final String PROTECTED_ENUM 		= "ProtectedEnumDefinition";
	public static final String PRIVATE_ENUM 		= "PrivateEnumDefinition";
	public static final String INTERNAL_ENUM 		= "InternalEnumDefinition";

	public static final String CONSTRUCTOR = "Constructor";

	public static final String CALLABLE = "CallableDefinition";

	public static final String STRUCT = "StructDefinition";

	public static final String EVENT = "Event";

	private String _name;

	private ArrayList<OutlineNode> _children;
	
	private OutlineNode _parent;

	private String _type;

	private int _line;

	private String _visibility = "";

	public OutlineNode() {
	}	
	
	private OutlineNode(OutlineNode parent) {
		_parent = parent;
	}
	
	public OutlineNode create() {
		OutlineNode child = new OutlineNode(this);
		add(child);
		return child;
	}
	
	private void add(OutlineNode child) {
		if (null == _children) _children = new ArrayList();
		_children.add(child);
		
		Collections.sort (_children, new Comparator() {  
            public int compare(Object o1, Object o2) {  
            	OutlineNode p1 = (OutlineNode) o1;  
            	OutlineNode p2 = (OutlineNode) o2;  
                return sortNode(p1, p2);
            }
		});
	}

	private int sortNode(OutlineNode p1, OutlineNode p2) {
		int index1 = getSortIndex(p1.type());
		int index2 = getSortIndex(p2.type());
				
		return  index1 < index2 ? -1 : (index1 > index2 ? 1 : p1.name().compareTo(p2.name()));
	}

	private int getSortIndex(String type) {
		HashMap<String, Integer> mapping = new HashMap<String, Integer>();
		
		mapping.put("InterfaceDefinition", 1);
		mapping.put("Protected_InterfaceDefinition", 1);
		mapping.put("Private_InterfaceDefinition", 1);
		mapping.put("Internal_InterfaceDefinition", 1);

		mapping.put("ClassDefinition", 2);
		mapping.put("Protected_ClassDefinition", 2);
		mapping.put("Private_ClassDefinition", 2);
		mapping.put("Internal_ClassDefinition", 2);
				
		mapping.put("Event", 3);
		mapping.put("CallableDefinition", 4);
		mapping.put("StructDefinition", 5);

		mapping.put("EnumDefinition", 6);
		mapping.put("ProtectedEnumDefinition", 6);
		mapping.put("PrivateEnumDefinition", 6);
		mapping.put("InternalEnumDefinition", 6);
		
		mapping.put("Field", 7);
		mapping.put("Protected_Field", 7);
		mapping.put("Private_Field", 7);
		mapping.put("Internal_Field", 7);

		mapping.put("Property", 8);
		mapping.put("Protected_Property", 8);
		mapping.put("Private_Property", 8);
		mapping.put("Internal_Property", 8);
		
		mapping.put("Constructor", 9);

		mapping.put("Method", 10);
		mapping.put("Protected_Method", 10);
		mapping.put("Private_Method", 10);
		mapping.put("Internal_Method", 10);

		return (mapping.containsKey(type) ? mapping.get(type) : 100);
	}  

	public OutlineNode parent() {
		return _parent;
	}

	public OutlineNode[] children() {
		if (null == _children) return NO_CHILDREN;
		return (OutlineNode[]) _children.toArray(new OutlineNode[_children.size()]);
	}

	public String name() {
		return _name;
	}
	
	public void name(String name) {
		_name = name;
	}
	
	public void type(String type) {
		_type = type;
	}

	public String type() {
		return _type;
	}

	public int line() {
		return _line;
	}
	
	public void line(int line) {
		_line = line;
	}

	public void visibility(String visibilty) {
		_visibility = visibilty;
	}
	
	public String visibility() {
		return _visibility;
	}
	
	public interface Visitor {
		boolean visit(OutlineNode node);
	}
	
	public boolean accept(Visitor visitor) {
		if (!visitor.visit(this))
			return false;
	
		if (null != _children)
			for (OutlineNode child : _children)
				if (!child.accept(visitor))
					return false;
		
		return true;
	}
}
