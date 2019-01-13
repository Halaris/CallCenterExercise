package com.evaluacion.callcenter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Basic callcenter application for an exercise.
 * <p>
 * This application doesn't expose any endpoints. It only have an internal dispatcher class
 * with the needed classes to work.
 * </p>
 * <p>
 * In order to validate it's work, there is a test named {@link AsyncDispatcherTests}
 * that will run some basic tests to validate the functionality of the dispatcher.
 * The explanation of the tests and the classes are as documents of the corresponding
 *	Classes and methods.
 *	</p>
 *	This project it's built with maven. In order to test it, it's needed to run the maven goals.
 *	Built with maven 3.5.4 and Java 1.8.181
 * @author Alexis Sessarego
 *
 */
@SpringBootApplication
public class CallcenterApplication {

	public static void main(String[] args) {
		SpringApplication.run(CallcenterApplication.class, args);
	}

}

