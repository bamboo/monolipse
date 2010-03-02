package monolipse.core.compiler;

import java.util.ArrayList;

public class OutlineNode {
	
	public static final OutlineNode[] NO_CHILDREN = new OutlineNode[0];
	
	public static final String CLASS = "ClassDefinition";
	
	public static final String METHOD = "Method";

	public static final String CONSTRUCTOR = "Constructor";

	public static final String FIELD = "Field";

	public static final String PROPERTY = "Property";

	public static final String INTERFACE = "InterfaceDefinition";

	public static final String CALLABLE = "CallableDefinition";

	public static final String STRUCT = "StructDefinition";

	public static final String ENUM = "EnumDefinition";

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
