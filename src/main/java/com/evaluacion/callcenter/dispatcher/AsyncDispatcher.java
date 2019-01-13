package com.evaluacion.callcenter.dispatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.evaluacion.callcenter.model.Call;
import com.evaluacion.callcenter.model.CallResult;
import com.evaluacion.callcenter.model.DelayedCall;
import com.evaluacion.callcenter.model.Employee;
import com.evaluacion.callcenter.model.EmployeeState;
import com.evaluacion.callcenter.model.Reason;

/**
 * 	AsyncDispatcher is an example class that allows to dispatch calls to a group of employees.
 * 	<p> This class uses a {@code ExecutorService} to dispatch the calls on different threads.
 *	On this specific implementation, a {@code fixedThreadPool} is used (for the requirements of the exercise)
 *	but it implements a setter that allows to use another {@code ExecutorService}.</p>
 *	<p>It's possible to change the number of threads, without changing the {@code ExecutorService},
 *	by modifying the value of the {@code AsyncDispatcher.maxThreads} variable in the application.Properties file of spring.</p>
 *	<p>The bean assigned to the Spring environment doesn't have any employee for default. Those need to be filled in order
 *	to allow the proper work of the dispatcher. If no employee is supplied, the dispatcher is going to delay the calls
 *	until a list of employees is assigned to this dispatcher.</p>
 * @author Alexis Sessarego
 *
 */
@Service
public class AsyncDispatcher {
	
	@Value("${AsyncDispatcher.maxThreads:10}")
	private int MAX_THREADS;
	private ExecutorService executor;
    private int threadsUsed = 0;
    private List<Employee> employees = new ArrayList<Employee>();
    private List<DelayedCall> delayedCalls = new ArrayList<DelayedCall>();
    Logger logger = LoggerFactory.getLogger(AsyncDispatcher.class);
    
    
    /** Tries to queue a call to be taken by an employee.
     * 	<ul>
     * 	<li>If there is no free threads, the call is returned without being attended and with an explanation of the problem</li>
     * 	<li>If there is no free employee, the call is delayed through 10 seconds(see {@link  DelayedCall#delayCall(Call, AsyncDispatcher)}). If no employee get freed during that time, 
     * 	the call is returned without being attended and with an explanation of the problem.</li>
     * 	<li>If a employee is freed while there is a delayed call, that call will be attended.</li>
     * 	<li>If there is a free employee when a call is received, the call gets attended by the employee (and the employee is flaged as occupied)
     * 	and a CompletableFuture is returned in order to deliver the information of the  call when it finishes.</li></ul>
     * 	The employees attend calls in the following order:
     * 	{@code OPERATOR}
	 *	{@code SUPERVISOR}
	 *	{@code DIRECTOR}
	 *	This order is defined in the enum {@link EmployeePosition}
     * @param event External call that needs to be attended by the employee.
     * @return a future with the call with the result and reason fields filled (if needed)
     */
    public CompletableFuture<Call> DispatchCall(Call event) {
    	logger.debug("Dispatcher receiving call: " + event.getId());
    	CompletableFuture<Call> future;
    	if(threadsUsed >= MAX_THREADS) { 
    		event.setResult(CallResult.OCUPPIED);
    		logger.debug("All threads already in use");
    		event.setReason(Reason.NO_FREE_LINE);
    		return CompletableFuture.completedFuture(event);
    	}
    	Optional<Employee> freeEmployee = getFreeEmployee();
    	if(freeEmployee.isPresent()) {
    	future = this.ExecuteAsync(() -> freeEmployee.get().atenderLlamada(event)).whenComplete((ev,ex)->{
    		logger.trace("Freeing thread");
    		this.freeThread();
    	});
    	freeEmployee.get().changeState();
    	} else {
    		DelayedCall delayedCall = new DelayedCall();
    		delayedCalls.add(delayedCall);
    		
    		future = this.ExecuteAsync(()-> {try {
				return delayedCall.delayCall(event, this);
			} catch (InterruptedException | ExecutionException e1) {
				event.setResult(CallResult.OCUPPIED);
	    		event.setReason(Reason.NO_FREE_EMPLOYEE);
	    		return event;
			}}).whenComplete((ev,ex)->{
				logger.trace("Freeing thread");
	    		this.freeThread();
	    	});
    	}

    	++threadsUsed;
    	return future;
    	
    }
    
    public Optional<Employee> getFreeEmployee() {
    	return employees.stream().filter((e) -> EmployeeState.FREE.equals(e.getState())).findFirst();
    }
    
    private void  freeThread() {
    	--threadsUsed;
    	if(!delayedCalls.isEmpty()) {
        	DelayedCall call = delayedCalls.get(0);
        	call.wakeCall();
        	delayedCalls.remove(0);
    	}
    }
    
    public void removeTerminatedDelayedCall(DelayedCall call) {
    	delayedCalls.remove(call);
    }
    
    private CompletableFuture<Call> ExecuteAsync(Supplier<Call> suplier) {
    	return CompletableFuture.supplyAsync(suplier, getExecutor());
    }

	private ExecutorService getExecutor() {
		if(executor == null) {
			executor = Executors.newFixedThreadPool(MAX_THREADS);
		}
		return executor;
	}

	public void setExecutor(ExecutorService executor) {
		this.executor = executor;
	}

	public List<Employee> getEmployees() {
		return employees;
	}


	/**	
	 * 	Assign a list of employees to this dispatcher, replacing any old one that was assigned to this dispatcher.
	 * 	<ul>
	 *	<li>If no list is supplied, the method returns without doing nothing.</li>
	 *	<li>If a list with employees is supplied, this method first sort the list by {@link EmployeePosition}
	 *	and then, it wakes the first delayed call, if there is one.</li>
	 *	</ul>
	 * @param employees List of new employees to be assigned to this dispatcher
	 */
	public void setEmployees(List<Employee> employees) {
		if(employees.isEmpty()) {
			return;
		}
		this.employees = employees.stream().sorted().collect(Collectors.toList());
		if(!delayedCalls.isEmpty()) {
        	DelayedCall call = delayedCalls.get(0);
        	call.wakeCall();
        	delayedCalls.remove(0);
    	}
	}

}
