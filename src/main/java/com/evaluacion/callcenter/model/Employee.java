package com.evaluacion.callcenter.model;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.evaluacion.callcenter.dispatcher.AsyncDispatcher;

/**
 * Class that represent an employee on this exercise.
 * <p>
 *  This class represents 3 different types of Employees.
 *  <ul>
 *  <li>OPERATOR</li>
 *	<li>SUPERVISOR</li>
 *	<li>DIRECTOR</li>
 *  </ul>
 *  This different types are represented by the {@link EmployeePosition} enum.
 *  This can also be achieved whit inheritance, having sub-classes that inherit from this class
 *  allowing them to have a particular implementations of  their functions.</p>
 *  <p>
 *  This can still be achieved, using this same class as an abstract parent class of those.
 *  The important note here, is the using of a enum to represent the different types of employees.
 *  This allows the dispatcher to easily sort a list of employees by their position.
 * </p>
 * @author Alexis Sessarego
 *
 */
public class Employee implements Comparable<Employee> {
	
	private int id;
	private EmployeeState state = EmployeeState.FREE;
	private EmployeePosition position;
	private Call atendedEvent;
    Logger logger = LoggerFactory.getLogger(Employee.class);
	
	
	public Call atenderLlamada(Call event) {
		Random randGen = new Random();
		long callDelay = (long)(randGen.nextDouble() * (1000 * 10 - 1000 * 5) + (1000 * 5 + 1L));
		logger.debug("Starting call: ----------Employee: " + this.toString() +" Call: " + event.getId() + " with delay: " + callDelay);
		this.setAtendedEvent(event);
		try {
			Thread.sleep(callDelay);
			event.setResult(CallResult.OK);
		} catch (InterruptedException e) {
			System.out.println("Hubo un error");
			event.setReason(Reason.ERROR_DURING_CALL);
			event.setResult(CallResult.PROBLEM);
		}
		state = EmployeeState.FREE;
		this.setAtendedEvent(null);
		logger.debug("Ending call: ----------Employee: " + this.toString() +" Call: " + event.getId());
		return event;
	}

	public void changeState() {
		if(state.equals(EmployeeState.FREE)) {
			state = EmployeeState.OCUPIED;
		} else {
			state = EmployeeState.FREE;
		}
	}
	
	@Override
	public int compareTo(Employee other) {
	    return this.getPosition().compareTo(other.getPosition());
	}

	@Override
	public String toString() {
		return "Empleado [id=" + id + ", state=" + state + ", position=" + position + ", atendedEvent=" + atendedEvent
				+ "]";
	}

	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public EmployeeState getState() {
		return state;
	}


	public void setState(EmployeeState state) {
		this.state = state;
	}

	public EmployeePosition getPosition() {
		return position;
	}

	public void setPosition(EmployeePosition position) {
		this.position = position;
	}

	/**
	 * @return the atendedEvent
	 */
	public Call getAtendedEvent() {
		return atendedEvent;
	}

	/**
	 * @param atendedEvent the atendedEvent to set
	 */
	public void setAtendedEvent(Call atendedEvent) {
		this.atendedEvent = atendedEvent;
	}

}
