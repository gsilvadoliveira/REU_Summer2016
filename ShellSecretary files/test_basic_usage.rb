require_relative '../test_helper'

class TestBasicUsage < Minitest::Test

  def test_minimum_arguments_required
    shell_output = ""
    expected_output = ""
    IO.popen('./shell_secretary') do |pipe|
      expected_output = "[Help] Run as: ./shell_secretary [manage]\n"
      shell_output = pipe.read
    end
    assert_equal expected_output, shell_output
  end

  def test_manage_multiple_arguments_given
    shell_output = ""
    expected_output = ""
    IO.popen('./shell_secretary manage blah') do |pipe|
      expected_output = "[Help] Run as: ./shell_secretary [manage]\n"
      shell_output = pipe.read
    end
    assert_equal expected_output, shell_output
  end


  def test_invalid_argument
    shell_output = ""
    expected_output = ""
    IO.popen('./shell_secretary foo') do |pipe|
      expected_output = "[Help] Run as: ./shell_secretary [manage]\n"
      shell_output = pipe.read
    end
    assert_equal expected_output, shell_output
  end

  def test_manage_argument_given_then_exit
    shell_output = ""
    expected_output = ""
    IO.popen('./shell_secretary manage', 'r+') do |pipe|
      expected_output = <<EOS
1. Add a new engagement to your calendar
2. List all upcoming engagements
3. Exit
EOS
      pipe.puts "3"
      expected_output << "Program quit\n"
      pipe.close_write
      shell_output = pipe.read
    end
    assert_equal expected_output, shell_output
  end

    def test_manage_list_engagements_when_DB_empty
      shell_output = ""
      expected_output = ""
      IO.popen('./shell_secretary manage', 'r+') do |pipe|
        expected_output = <<EOS
1. Add a new engagement to your calendar
2. List all upcoming engagements
3. Exit
EOS
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
end
