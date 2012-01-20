Feature: Login
	Write here a description of how it should work...

	Background:
    	Given the system is configured
		And I am not logged in

	Scenario: Display login page if not logged in
		When I access the home page
		Then I should be redirected to the login page

	Scenario: Login with invalid credentials
		Given I am on the login page
		And I have filled the login form with invalid data
		When I press the login button
		Then it should fail with a popup window
