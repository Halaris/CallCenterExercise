package com.evaluacion.callcenter.model;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.evaluacion.callcenter.dispatcher.AsyncDispatcher;

/**
 * Class representing a call that is being delayed by the dispatcher.
 * 
 * @author Alexis Sessarego
 *
 */
public class DelayedCall {
	
	private int id;
	private boolean notified = false;
	@Value("${DelayedCall.delay:10}")
	private int delay;
    Logger logger = LoggerFactory.getLogger(DelayedCall.class);
	/**	Delays a call for a number of seconds equals to {@code delay} (10 by default).
	 * If the call is not attended for that time, the same is returned as not attended.
	 * @param call
	 * @param dispatcher
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public synchronized Call delayCall(Call call, AsyncDispatcher dispatcher) throws InterruptedException, ExecutionException{
		try {
			this.wait(delay*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(notified == false) {
			logger.warn("Timeout of delayed  call, returning call unantended");
			call.setReason(Reason.NO_FREE_EMPLOYEE);
			call.setResult(CallResult.OCUPPIED);
			dispatcher.removeTerminatedDelayedCall(this);
			return call;
		}
		Optional<Employee> freeEmployee = dispatcher.getFreeEmployee();
    	if(freeEmployee.isPresent()) {
    		freeEmployee.get().changeState();
    		return freeEmployee.get().atenderLlamada(call);
    	} else {
    		call.setReason(Reason.NO_FREE_EMPLOYEE);
			call.setResult(CallResult.OCUPPIED);
			return call;
    	}
	}
	
	public synchronized void wakeCall() {
		this.notified = true;
		this.notify();
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the delay
	 */
	public int getDelay() {
		return delay;
	}

	/**
	 * @param delay the delay to set
	 */
	public void setDelay(int delay) {
		this.delay = delay;
	}

}
