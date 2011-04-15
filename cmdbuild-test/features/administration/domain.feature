Feature: Domain administration
	In order to create relations between cards
	an Administrator must be able to define domains between classes
	
	Background:
    	Given the system is configured with a basic class structure
		And I am logged in as an administrator
		And I am on the domain administration section
	
	@wip
	Scenario: Existing domains are listed
		Then I should see the list of the existing domains