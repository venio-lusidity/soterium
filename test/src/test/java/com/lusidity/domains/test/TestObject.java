package com.lusidity.domains.test;

import com.lusidity.annotations.AtSchemaProperty;
import com.lusidity.collections.Edges;
import com.lusidity.collections.ElementEdges;
import com.lusidity.collections.NodeEdges;
import com.lusidity.domains.BaseDomain;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.json.JsonData;

@AtSchemaClass(name="Test Object", discoverable = true, writable = true)
public class TestObject extends BaseDomain
{
	@AtSchemaProperty(name="Test Objects", expectedType = TestObject.class, fieldName = "testObjects")
	private Edges<TestObject> testObjects = null;

	@AtSchemaProperty(name="Element Test Objects", expectedType = TestObject.class, fieldName = "testObjects")
	private ElementEdges<TestObject> elements = null;

	@AtSchemaProperty(name="Node Element Test Objects", expectedType = TestObject.class, fieldName = "testObjects")
	private NodeEdges<TestObject> nodeElements = null;

	public TestObject(){
		super();
	}

	public TestObject(JsonData dso, Object indexId){
		super(dso, indexId);
	}

	public Edges<TestObject> getTestObjects()
	{
		if(null==this.testObjects){
			this.buildProperty("testObjects");
		}
		return this.testObjects;
	}

	public ElementEdges<TestObject> getElements()
	{
		if(null==this.elements){
			this.buildProperty("elements");
		}
		return this.elements;
	}

	public NodeEdges<TestObject> getNodeElements()
	{
		if(null==this.nodeElements){
			this.buildProperty("nodeElements");
		}
		return this.nodeElements;
	}
}
