package org.dyn4j.samples;

import org.dyn4j.dynamics.joint.RevoluteJoint;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.samples.framework.SimulationBody;
import org.dyn4j.samples.framework.SimulationFrame;

public class Intake extends SimulationFrame {

	public Intake() {
		super("Intake", 32.0);
		this.setOffsetY(-200);
	}

	protected void initializeWorld() {

		SimulationBody ground = new SimulationBody();
		ground.addFixture(Geometry.createRectangle(10, 0.1));
		ground.translate(new Vector2(0.0, 0.0));
		ground.setMass(MassType.INFINITE);
		world.addBody(ground);

		SimulationBody frame = new SimulationBody();
		frame.addFixture(Geometry.createRectangle(0.5, 5.0));
		frame.translate(new Vector2(-2.50, 2.5));
		frame.setMass(MassType.NORMAL);
		world.addBody(frame);

		SimulationBody arm = new SimulationBody();
		arm.addFixture(Geometry.createRectangle(0.5, 5.0), 2.0, 0.0, 0.0);
		arm.translate(new Vector2(2.5, 2.5));
		arm.setMass(MassType.NORMAL);
		world.addBody(arm);

		SimulationBody link3 = new SimulationBody();
		link3.addFixture(Geometry.createRectangle(5.0, 0.5));
		link3.translate(new Vector2(0.0, 5.0));
		link3.setMass(MassType.NORMAL);
		world.addBody(link3);

		RevoluteJoint<SimulationBody> pivot = new RevoluteJoint<SimulationBody>(arm, ground,new Vector2(1.0, 1.0));
		world.addJoint(pivot);

		RevoluteJoint<SimulationBody> pivot4 = new RevoluteJoint<SimulationBody>(frame, ground,new Vector2(-1.0, 1.0));
		world.addJoint(pivot4);

		RevoluteJoint<SimulationBody> pivot2 = new RevoluteJoint<SimulationBody>(link3, arm, new Vector2(0, 5));
		world.addJoint(pivot2);

		RevoluteJoint<SimulationBody> pivot3 = new RevoluteJoint<SimulationBody>(link3, frame, new Vector2(3, 5));
		world.addJoint(pivot3);

	}

	@Override
	protected void handleEvents() {
		super.handleEvents();
		// System.out.println("hi");
	}

	public static void main(String[] args) {
		Intake simulation = new Intake();
		simulation.run();

	}
}
