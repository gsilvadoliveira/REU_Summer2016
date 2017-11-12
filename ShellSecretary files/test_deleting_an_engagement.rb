require_relative '../test_helper'

class TestDeletingEngagements < Minitest::Test

  def test_happy_path_deleting_an_engagement
    shell_output = ""
    expected_output = main_menu
    test_engagement = "run with scissors"
    engagement = Engagement.new(test_engagement)
    engagement.save
    IO.popen('./shell_secretary manage', 'r+') do |pipe|
      pipe.puts "2" # List
      expected_output << "1. #{test_engagement}\n"
      expected_output << "2. Exit\n"
      pipe.puts "1"
      expected_output << actions_menu
      pipe.puts "2" # Delete
      expected_output << "#{test_engagement} has been removed\n"
      expected_output << main_menu
      pipe.puts "2" #list
      expected_output << "No engagements found. Add an engagement.\n"
      expected_output << main_menu
      pipe.puts "3" # Exit
      expected_output << "Program quit\n"
      shell_output = pipe.read
      pipe.close_write
      pipe.close_read
    end
    assert_equal expected_output, shell_output
  end

end