package org.dyn4j.samples;

import org.dyn4j.dynamics.joint.DistanceJoint;
import org.dyn4j.dynamics.joint.RevoluteJoint;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.samples.framework.SimulationBody;
import org.dyn4j.samples.framework.SimulationFrame;

/**
 * Demonstrates a four-bar intake linkage with a roller.
 */
public class Intake extends SimulationFrame {

	public Intake() {
		super("Intake", 32.0);
		this.setOffsetY(-200);
	}

	protected void initializeWorld() {

		SimulationBody ground = new SimulationBody();
		ground.addFixture(Geometry.createRectangle(50, 0.1));
		ground.translate(new Vector2(0.0, -5.0));
		ground.setMass(MassType.INFINITE);
		world.addBody(ground);

		// (2.5, 2.5) to (2.5, 7.5)
		SimulationBody right = new SimulationBody();
		right.addFixture(Geometry.createRectangle(0.5, 5));
		right.translate(new Vector2(2.5, 5));
		right.setMass(MassType.NORMAL);
		world.addBody(right);

		SimulationBody lowerLink = new SimulationBody();
		lowerLink.addFixture(Geometry.createRectangle(0.5, 5));
		lowerLink.translate(new Vector2(2.5, 0));
		lowerLink.setMass(MassType.NORMAL);
		world.addBody(lowerLink);

		RevoluteJoint<SimulationBody> lowerPivot = new RevoluteJoint<SimulationBody>(
				lowerLink, right, new Vector2(2.5, 2.5));
		world.addJoint(lowerPivot);

		RevoluteJoint<SimulationBody> lowerInnerPivot = new RevoluteJoint<SimulationBody>(
				lowerLink, ground, new Vector2(2.5, -2.5));
		lowerInnerPivot.setLimits(-Math.PI / 2, 0);
		lowerInnerPivot.setLimitEnabled(true);
		world.addJoint(lowerInnerPivot);

		// A distance joint is easier than diagonal rectangle. :-)
		DistanceJoint<SimulationBody> upper = new DistanceJoint<SimulationBody>(
				ground, right, new Vector2(-2, 2.5), new Vector2(2.5, 7.5));
		upper.setFrequency(0);
		world.addJoint(upper);

		// this is the simple bad kind of spring
		// TODO: use the pulley kind
		DistanceJoint<SimulationBody> spring = new DistanceJoint<SimulationBody>(
				ground, right, new Vector2(-2, 2.5), new Vector2(2.5, 2.5));
		spring.setFrequency(0.25);
		world.addJoint(spring);

		SimulationBody ball = new SimulationBody();
		ball.addFixture(Geometry.createCircle(1));
		ball.translate(new Vector2(5, 5));
		ball.setMass(MassType.NORMAL);
		world.addBody(ball);

		SimulationBody roller = new SimulationBody();
		roller.addFixture(Geometry.createCircle(1));
		roller.translate(new Vector2(2.5, 2.5));
		roller.setMass(MassType.NORMAL);
		world.addBody(roller);

		RevoluteJoint<SimulationBody> rollerJoint = new RevoluteJoint<SimulationBody>(
				right, roller, new Vector2(2.5, 2.5));
		rollerJoint.setMotorEnabled(true);
		rollerJoint.setMotorSpeed(50);
		rollerJoint.setMaximumMotorTorque(100);
		world.addJoint(rollerJoint);
	}

	public static void main(String[] args) {
		Intake simulation = new Intake();
		simulation.run();

	}
}
