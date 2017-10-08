package com.vitter.rest;

import static spark.Spark.*;

/**
 * Hello World REST Server
 */
public class Main {
	
	// Unused variables to trigger PMD warnings
	private String blahBlahBlah = "Some blah blah blah!";
	private int one = 1;
	
	
	public static void main(String[] args) {
		//
		System.out.println("Starting our simple rest server...");
		System.out.println("Hello World Example: http://localhost:4567/hello");
		
		// Basic Hello World get example:
        get("/hello", (req, res) -> "Que pasa!");
    }
	
}
