Feature: Domain administration
	In order to create relations between cards
	an Administrator must be able to define domains between classes

	Background:
		Given the system is configured with a basic class structure
		And I am logged in as an administrator
		And I am on the domain administration section

	Scenario: Existing domains are listed
		Then I should see the list of the existing domains

	@pending
	Scenario: Fill the form with the selected domain properties
		When I select a domain from the list
		Then I should see the properties of the domain in the form
		And the modify and delete buttons should be disabled

	@pending
	Scenario: Add a domain
		When I click the add button
		And I fill the form
		And I click the save button
		Then I should see the new domain in the tree
		And the form should be disabled

	@pending
	Scenario: Delete a domain
		When I click the remove button
		And confirm my intention to delete the domain
		Then I should see that the domain was removed from the list
		And the list is unselected
		And the form is disabled
		And all the buttons are disabled

