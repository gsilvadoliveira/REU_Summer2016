require_relative '../test_helper'

class AddingANewEngagementTest < Minitest::Test
  def test_happy_path_adding_a_engagement
    shell_output = ""
    expected_output = main_menu
    test_engagement = "9/9/2015 Happy Birthday"
    IO.popen('./shell_secretary manage', 'r+') do |pipe|
      pipe.puts "1"
      expected_output << "what engagement would you like to add?\n"
      pipe.puts test_engagement
      expected_output << "\"#{test_engagement}\" has been added\n"
      expected_output << main_menu
      pipe.puts "2"
      expected_output << "1. #{test_engagement}\n2. Exit\n"
      expected_output << exit_from(pipe)
      shell_output = pipe.read
      pipe.close_write
      pipe.close_read
    end
    assert_equal expected_output, shell_output
  end

  def test_sad_path_adding_a_engagement
    shell_output = ""
    test_engagement = "9/9/2015 Happy Birthday"
    expected_output = main_menu
    IO.popen('./shell_secretary manage', 'r+') do |pipe|
      pipe.puts "1"
      expected_output << "what engagement would you like to add?\n"
      pipe.puts ""
      expected_output << "\"\" is not a valid engagement name.\n"
      expected_output << "what engagement would you like to add?\n"
      pipe.puts test_engagement
      expected_output << "\"#{test_engagement}\" has been added\n"
      expected_output << main_menu
      pipe.puts "2"
      expected_output << "1. #{test_engagement}\n2. Exit\n"
      expected_output << exit_from(pipe)
      shell_output = pipe.read
      pipe.close_write
      pipe.close_read
    end
    assert_equal expected_output, shell_output
  end
end