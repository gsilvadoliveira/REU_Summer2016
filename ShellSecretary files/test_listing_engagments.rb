require_relative '../test_helper'

class TestListingEngagements < Minitest::Test

  def test_listing_no_engagements
    shell_output = ""
    expected_output = ""
    IO.popen('./shell_secretary manage', 'r+') do |pipe|
      expected_output << main_menu
      pipe.puts "2"
      expected_output << "No engagements found. Add an engagement.\n"
      expected_output << main_menu
      pipe.puts "3"
      expected_output << "Program quit\n"
      pipe.close_write
      shell_output = pipe.read
    end
    assert_equal expected_output, shell_output
  end

  def test_listing_multiple_engagements
    log_engagements("09/09/2015 Happy Birthday")
    log_engagements("05/05/2015 Haircut @ Parlor & Juke w/ Max")
    shell_output = ""
    expected_output = ""
    IO.popen('./shell_secretary manage', 'r+') do |pipe|
      expected_output << main_menu
      pipe.puts "2" # List all engagements
      expected_output << "1. 09/09/2015 Happy Birthday\n"
      expected_output << "2. 05/05/2015 Haircut @ Parlor & Juke w/ Max\n"
      expected_output << "3. Exit\n"
      pipe.puts "Exit"
      expected_output << "Exit\n"
      expected_output << main_menu
      pipe.puts "3"
      expected_output << "Program quit\n"
      pipe.close_write
      shell_output = pipe.read
    end
    assert_equal expected_output, shell_output
  end
end