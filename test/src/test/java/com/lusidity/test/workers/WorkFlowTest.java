package com.lusidity.test.workers;

import com.lusidity.domains.acs.security.AnonymousCredentials;
import com.lusidity.domains.acs.security.Identity;
import com.lusidity.domains.acs.security.authorization.Group;
import com.lusidity.domains.object.Edge;
import com.lusidity.domains.object.EdgeData;
import com.lusidity.domains.people.Person;
import com.lusidity.domains.process.workflow.Workflow;
import com.lusidity.domains.process.workflow.WorkflowStep;
import com.lusidity.framework.data.Common;
import com.lusidity.test.BaseTest;
import com.lusidity.test.acs.TestUserCredentials;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Objects;

public class WorkFlowTest extends BaseTest
{
	private static final String IDENTIFIER_1= "1041216150";
	private static final String IDENTIFIER_2= "1041216151";
	private static final String IDENTIFIER_3= "1041216152";
	private static final String IDENTIFIER_4= "1041216153";
	private static final String PROVIDER= "x509";

	@BeforeClass
	public static
	void beforeClass() throws Exception
	{
		BaseTest.setInitialize(true);
		BaseTest.beforeClass();
	}

	@Override
	public
	boolean isDisabled()
	{
		return true;
	}

	@Test
	public void sequentialApproved()
		throws Exception
	{
		if (!this.isDisabled())
		{
			Group issm=new Group();
			issm.fetchTitle().setValue("issm");
			issm.save();

			Group directorate=new Group();
			directorate.fetchTitle().setValue("directorate");
			directorate.save();

			Group aor=new Group();
			aor.fetchTitle().setValue("ao rep");
			aor.save();

			// First Person
			Person initiator=this.getPrincipal(WorkFlowTest.PROVIDER, WorkFlowTest.IDENTIFIER_1, issm);

			// Second Person
			Person dPerson=this.getPrincipal(WorkFlowTest.PROVIDER, WorkFlowTest.IDENTIFIER_2, directorate);

			// Third Person
			Person aPerson=this.getPrincipal(WorkFlowTest.PROVIDER, WorkFlowTest.IDENTIFIER_3, aor);

			TestUserCredentials credentials=new TestUserCredentials(initiator.getIdentities().get(), initiator);
			TestUserCredentials credentials2=new TestUserCredentials(dPerson.getIdentities().get(), dPerson);
			TestUserCredentials credentials3=new TestUserCredentials(aPerson.getIdentities().get(), aPerson);

			issm.setCredentials(credentials);
			directorate.setCredentials(credentials);
			aor.setCredentials(credentials);

			Workflow workflow=new Workflow();
			workflow.fetchTitle().setValue("Plan of Action and Milestones");
			workflow.save();
			workflow.setCredentials(credentials);

			WorkflowStep step1=new WorkflowStep();
			step1.fetchTitle().setValue("step_1");
			step1.fetchActionType().setValue(WorkflowStep.ActionTypes.approve);
			step1.save();
			step1.getAuditors().add(issm);
			step1.setCredentials(credentials);

			EdgeData edgeData=new EdgeData();
			edgeData.setFromOrdinal(0L);
			workflow.getSteps().add(step1, edgeData, null);

			WorkflowStep step2=new WorkflowStep();
			step2.fetchTitle().setValue("step_2");
			step2.fetchActionType().setValue(WorkflowStep.ActionTypes.approve);
			step2.save();
			step2.getAuditors().add(directorate);
			step2.setCredentials(credentials);

			edgeData=new EdgeData();
			edgeData.setFromOrdinal(1L);
			workflow.getSteps().add(step2, edgeData, null);

			WorkflowStep step3=new WorkflowStep();
			step3.fetchTitle().setValue("step_3");
			step3.fetchActionType().setValue(WorkflowStep.ActionTypes.approve);
			step3.save();
			step3.getAuditors().add(directorate);

			edgeData=new EdgeData();
			edgeData.setFromOrdinal(2L);
			workflow.getSteps().add(step3, edgeData, null);
			step3.setCredentials(credentials);

			String key=workflow.getSteps().getKey();
			Class<? extends Edge> et=workflow.getSteps().getEdgeType();

			long on=0;
			for (WorkflowStep step : workflow.getSteps())
			{
				Edge edge=workflow.getEdgeHelper().getEdge(et, step, key, Common.Direction.OUT);
				Assert.assertNotNull("The edge should not be null.", edge);
				Long ordinal=edge.fetchEndpointFrom().getValue().fetchOrdinal().getValue();
				Assert.assertTrue("The ordinals do not match and the collection is out of order.", Objects.equals(on, ordinal));
				on++;
			}

			Assert.fail("This test needs to be refactored.");
/*
			Asset asset=new Asset();
			asset.fetchTitle().setValue("XBox One");
			asset.save();
			asset.setCredentials(credentials);

			POAM poam=new POAM();
			poam.fetchTitle().setValue("Plan of Action and Milestones");
			poam.save();
			poam.setCredentials(credentials);

			poam.getTargets().add(asset);
			poam.getInitiators().add(initiator);
			poam.getWorkflows().add(workflow);

			Assert.assertTrue("There should only be one step.", poam.getSteps().size()==1);

			//  process step 1
			WorkflowStep actual=poam.getSteps().get();
			Assert.assertEquals("The steps do not match.", actual.fetchOriginalId().getValue(), step1.fetchId().getValue());
			Assert.assertTrue("The parents workflow item should not be null and there should only be one.",
				actual.getParentWorkflowItem().size()==1
			);

			actual.fetchApproved().setValue(true);
			actual.save();

			//  process step 2
			actual=poam.getSteps().get();
			Assert.assertEquals("The steps do not match.", actual.fetchOriginalId().getValue(), step2.fetchId().getValue());
			Assert.assertTrue("The parents workflow item should not be null and there should only be one.",
				actual.getParentWorkflowItem().size()==1
			);

			actual.fetchApproved().setValue(true);
			actual.save();

			//  process step 3
			actual=poam.getSteps().get();
			Assert.assertEquals("The steps do not match.", actual.fetchOriginalId().getValue(), step3.fetchId().getValue());
			Assert.assertTrue("The parents workflow item should not be null and there should only be one.",
				actual.getParentWorkflowItem().size()==1
			);

			actual.fetchApproved().setValue(true);
			actual.save();

			Assert.assertTrue("The steps should be empty.", poam.getSteps().isEmpty());

			// workflow item should be completed.
			POAM finalPoam=
				Environment.getInstance().getDataStore().getObjectById(POAM.class, POAM.class, poam.fetchId().getValue());

			Assert.assertNotNull("The finalPoam should not be null.", finalPoam);
			Assert.assertTrue("The POAM should be completed.", finalPoam.fetchCompleted().isTrue());
			Assert.assertTrue("The POAM should be approved.", finalPoam.fetchApproved().isTrue());
			*/
		}
		else{
			System.out.print("This test has been disabled.");
		}
	}

	@Test
	public void sequentialWithDisapprove()
		throws Exception
	{
		if (!this.isDisabled())
		{
			Group issm=new Group();
			issm.fetchTitle().setValue("issm");
			issm.save();

			Group directorate=new Group();
			directorate.fetchTitle().setValue("directorate");
			directorate.save();

			Group aor=new Group();
			aor.fetchTitle().setValue("ao rep");
			aor.save();

			// First Person
			Person initiator=this.getPrincipal(WorkFlowTest.PROVIDER, WorkFlowTest.IDENTIFIER_1, issm);

			// Second Person
			Person dPerson=this.getPrincipal(WorkFlowTest.PROVIDER, WorkFlowTest.IDENTIFIER_2, directorate);

			// Third Person
			Person aPerson=this.getPrincipal(WorkFlowTest.PROVIDER, WorkFlowTest.IDENTIFIER_3, aor);

			TestUserCredentials credentials=new TestUserCredentials(initiator.getIdentities().get(), initiator);
			TestUserCredentials credentials2=new TestUserCredentials(dPerson.getIdentities().get(), dPerson);
			TestUserCredentials credentials3=new TestUserCredentials(aPerson.getIdentities().get(), aPerson);

			issm.setCredentials(credentials);
			directorate.setCredentials(credentials);
			aor.setCredentials(credentials);

			Workflow workflow=new Workflow();
			workflow.fetchTitle().setValue("Plan of Action and Milestones");
			workflow.save();
			workflow.setCredentials(credentials);

			WorkflowStep step1=new WorkflowStep();
			step1.fetchTitle().setValue("step_1");
			step1.fetchActionType().setValue(WorkflowStep.ActionTypes.approve);
			step1.save();
			step1.getAuditors().add(issm);
			step1.setCredentials(credentials);

			EdgeData edgeData=new EdgeData();
			edgeData.setFromOrdinal(0L);
			workflow.getSteps().add(step1, edgeData, null);

			WorkflowStep step2=new WorkflowStep();
			step2.fetchTitle().setValue("step_2");
			step2.fetchActionType().setValue(WorkflowStep.ActionTypes.approve);
			step2.save();
			step2.getAuditors().add(directorate);
			step2.setCredentials(credentials);

			edgeData=new EdgeData();
			edgeData.setFromOrdinal(1L);
			workflow.getSteps().add(step2, edgeData, null);

			WorkflowStep step3=new WorkflowStep();
			step3.fetchTitle().setValue("step_3");
			step3.fetchActionType().setValue(WorkflowStep.ActionTypes.approve);
			step3.save();
			step3.getAuditors().add(directorate);

			edgeData=new EdgeData();
			edgeData.setFromOrdinal(2L);
			workflow.getSteps().add(step3, edgeData, null);
			step3.setCredentials(credentials);

			String key=workflow.getSteps().getKey();
			Class<? extends Edge> et=workflow.getSteps().getEdgeType();

			long on=0;
			for (WorkflowStep step : workflow.getSteps())
			{
				Edge edge=workflow.getEdgeHelper().getEdge(et, step, key, Common.Direction.OUT);
				Assert.assertNotNull("The edge should not be null.", edge);
				Long ordinal=edge.fetchEndpointFrom().getValue().fetchOrdinal().getValue();
				Assert.assertTrue("The ordinals do not match and the collection is out of order.", Objects.equals(on, ordinal));
				on++;
			}

			Assert.fail("This test needs to be refactored.");
			/*
			Asset asset=new Asset();
			asset.fetchTitle().setValue("XBox One");
			asset.save();
			asset.setCredentials(credentials);

			POAM poam=new POAM();
			poam.fetchTitle().setValue("Plan of Action and Milestones");
			poam.save();
			poam.setCredentials(credentials);

			poam.getTargets().add(asset);
			poam.getInitiators().add(initiator);
			poam.getWorkflows().add(workflow);

			Assert.assertTrue("There should only be one step.", poam.getSteps().size()==1);

			//  process step 1
			WorkflowStep actual=poam.getSteps().get();
			Assert.assertEquals("The steps do not match.", actual.fetchOriginalId().getValue(), step1.fetchId().getValue());
			Assert.assertTrue("The parents workflow item should not be null and there should only be one.",
				actual.getParentWorkflowItem().size()==1
			);

			actual.fetchApproved().setValue(true);
			actual.save();

			//  process step 2
			actual=poam.getSteps().get();
			Assert.assertEquals("The steps do not match.", actual.fetchOriginalId().getValue(), step2.fetchId().getValue());
			Assert.assertTrue("The parents workflow item should not be null and there should only be one.",
				actual.getParentWorkflowItem().size()==1
			);

			actual.fetchApproved().setValue(false);
			actual.save();

			Assert.assertTrue("The steps should be empty.", poam.getSteps().isEmpty());

			POAM finalPoam=
				Environment.getInstance().getDataStore().getObjectById(POAM.class, POAM.class, poam.fetchId().getValue());

			Assert.assertNotNull("The finalPoam should not be null.", finalPoam);
			Assert.assertTrue("The POAM should be completed.", finalPoam.fetchCompleted().isTrue());
			Assert.assertTrue("The POAM should not be approved.", !finalPoam.fetchApproved().isTrue());
			*/
		}
		else{
			System.out.print("This test has been disabled.");
		}
	}

	@SuppressWarnings("Duplicates")
	private
	Person getPrincipal(String provider, String identifier, Group group)
		throws Exception
	{
		Assert.assertNotNull("The position id should not be null.", group.fetchId().getValue());

		Identity identity = Identity.get(provider, identifier);
		Assert.assertNotNull("The identity id should not be null.", identity.fetchId().getValue());

		Assert.assertNotNull("There should be an identity in the config file that is"+
		                     " used to create the one requested.", identity);
		Person result = (Person) identity.getPrincipal();
		Assert.assertNotNull("The principal should not be null.", result);
		result.setCredentials(new AnonymousCredentials());

		Assert.assertNotNull("The result id should not be null.", result.fetchId().getValue());

		boolean added = group.getPrincipals().add(result);

		Assert.assertTrue("The principal was not added to the position.", added);

		return result;
	}
}
