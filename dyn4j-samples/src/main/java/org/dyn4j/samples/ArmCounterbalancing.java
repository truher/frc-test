package org.dyn4j.samples;

import org.dyn4j.dynamics.joint.DistanceJoint;
import org.dyn4j.dynamics.joint.PulleyJoint;
import org.dyn4j.dynamics.joint.RevoluteJoint;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.samples.framework.SimulationBody;
import org.dyn4j.samples.framework.SimulationFrame;

/**
 * Shows two ways to do arm counterbalancing: one that works and one that
 * doesn't.
 */
public class ArmCounterbalancing {
	/* this only works with a spring of zero resting length */
	public static class SpringVersion extends ArmBase {
		protected void initializeWorld() {
			super.initializeWorld();

			DistanceJoint<SimulationBody> spring = new DistanceJoint<SimulationBody>(frame, arm,
					new Vector2(0.0, 5.0),
					new Vector2(0.0, 5.0));
			spring.setFrequency(0.5);
			spring.setRestDistance(2.0);
			world.addJoint(spring);
		}
	}

	/* this is the pulley way with realistic spring extension */
	public static class PulleyVersion extends ArmBase {
		protected void initializeWorld() {
			super.initializeWorld();

			SimulationBody junction = new SimulationBody();
			junction.setColor(java.awt.Color.RED);
			junction.addFixture(Geometry.createCircle(0.25), 1.0, 0.0, 0.0);
			junction.translate(new Vector2(0.0, 10.0));
			junction.setMass(MassType.NORMAL);
			world.addBody(junction);

			DistanceJoint<SimulationBody> spring = new DistanceJoint<SimulationBody>(frame, junction,
					new Vector2(0.0, 10.0),
					new Vector2(0.0, 10.0));
			spring.setFrequency(2.315);
			spring.setRestDistance(2.0); // spring resting length
			world.addJoint(spring);

			PulleyJoint<SimulationBody> string = new PulleyJoint<SimulationBody>(arm,
					junction,
					new Vector2(0.0, 3.0),
					new Vector2(0.0, 3.0), // pulley is even with the middle of the arm
					new Vector2(0.0, 1.0), // string goes to the middle of the arm
					new Vector2(0.0, 10.0) // string goes to junction
			);
			string.setLength(5); // preload
			world.addJoint(string);
		}
	}

	public static class ArmBase extends SimulationFrame {
		SimulationBody frame;
		SimulationBody arm;
		RevoluteJoint<SimulationBody> pivot;

		public ArmBase() {
			super("Example", 32.0);
			this.setOffsetY(-200);
		}

		protected void initializeWorld() {

			frame = new SimulationBody();
			frame.addFixture(Geometry.createRectangle(0.5, 10.0));
			frame.translate(new Vector2(0.0, 5.0));
			frame.setMass(MassType.INFINITE);
			world.addBody(frame);

			arm = new SimulationBody();
			arm.addFixture(Geometry.createRectangle(0.5, 5.0), 2.0, 0.0, 0.0);
			arm.translate(new Vector2(0.0, 2.5));
			arm.setMass(MassType.NORMAL);
			world.addBody(arm);

			pivot = new RevoluteJoint<SimulationBody>(frame, arm,
					new Vector2(0.0, 0.0));
			world.addJoint(pivot);
		}
	}

	public static void main(String[] args) {
		PulleyVersion simulation = new PulleyVersion();
		simulation.run();

		SpringVersion simulation2 = new SpringVersion();
		simulation2.setLocation(800, 0);
		simulation2.run();
	}
}
