package com.evaluacion.callcenter.dispatcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.evaluacion.callcenter.model.Call;
import com.evaluacion.callcenter.model.CallResult;
import com.evaluacion.callcenter.model.Employee;
import com.evaluacion.callcenter.model.EmployeePosition;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AsyncDispatcherTests {

	@Autowired
	AsyncDispatcher dispatcher;
	
	@Before
	public void load() {
		List<Employee> empleados = new ArrayList<>();
		Random randGen = new Random();
		for(int i = 0; i < 5; i ++) {
			Employee empleado = new Employee();
			empleado.setId(i);
			empleado.setPosition(EmployeePosition.values()[randGen.nextInt(EmployeePosition.values().length)]);
			empleados.add(empleado);
		}
		dispatcher.setEmployees(empleados);
	}
	
	/** This tests is in charge of validating that if the dispatcher receives more calls
	 * 	than the number of free employees that it have, but it still haves free threads,
	 * 	it will accept those calls and retain them until it have a free employee  to attend it.
	 */
	@Test
	public void testMoreCallsThanEmployee() {
		CompletableFuture<Call>[] calls = generateNCalls(10);
		CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(calls);
		try {
			combinedFuture.get();
			assertEquals(0, Arrays.stream(calls).map(CompletableFuture::join).filter((e)->  !e.getResult().equals(CallResult.OK)).count());
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			assertTrue("Interrupted call", Boolean.FALSE);
		}
	}
	
	/**This test validates that when the dispatcher receives more calls that the number of threads that it has,
	 *  it will cancel those calls, assigning to them the "OCUPPIED" state
	 */
	@Test
	public void testMoreCallsThanThreads() {
		CompletableFuture<Call>[] calls = generateNCalls(20);
		CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(calls);
		try {
			combinedFuture.get();
			Arrays.stream(calls).map(CompletableFuture::join).filter((e)->  !e.getResult().equals(CallResult.OK)).forEach((e)-> {
				assertEquals(CallResult.OCUPPIED, e.getResult());
				});
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			assertTrue("Interrupted call", Boolean.FALSE);
		}
	}
	
	/** Basic test.
	 * 	Only test that the dispatcher answers correctly to one call.
	 */
	@Test
	public void test1Calls() {
		CompletableFuture<Call>[] calls = generateNCalls(1);
		try {
			Call call = calls[0].get(10, TimeUnit.SECONDS);
			assertEquals(CallResult.OK, call.getResult());
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			assertTrue("Interrupted call", Boolean.FALSE);
		}
		//futures.stream().map(CompletableFuture::join).forEach((call) -> { System.out.println(call.getId() + ": " + call.getResult());});
	}

	@SuppressWarnings("unchecked")
	private CompletableFuture<Call>[] generateNCalls(int n) {
		CompletableFuture<Call>[] futures = new CompletableFuture[n];
		for(int i = 0; i < n; i ++) {
			Call call = new Call();
			call.setId(i);
			futures[i] = dispatcher.DispatchCall(call);	
		}
		return futures;
	}
	
}

