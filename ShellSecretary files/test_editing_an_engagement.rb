class EditingAEngagementTest < Minitest::Test

  def test_user_left_engagements_unchanged
    shell_output = ""
    expected_output = main_menu
    test_engagement = "run with scissors"
    Engagement.new(test_engagement).save
    IO.popen('./shell_secretary manage', 'r+') do |pipe|
      pipe.puts "2" # List
      expected_output << "1. #{test_engagement}\n"
      expected_output << "2. Exit\n"
      pipe.puts "1"
      expected_output << actions_menu
      pipe.puts "3" # Exit
      shell_output = pipe.read
      pipe.close_write
      pipe.close_read
    end
    assert_equal expected_output, shell_output
  end

  def test_happy_path_editing_a_engagement
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
      pipe.puts "1" # Edit
      expected_output << "Enter a new name:\n"
      pipe.puts "Eat a pop tart"
      expected_output << "Engagement has been updated to: \"Eat a pop tart\"\n"
      expected_output << main_menu
      pipe.puts "3" # Exit
      expected_output << "Program quit\n"
      shell_output = pipe.read
      pipe.close_write
      pipe.close_read
    end
    assert_equal expected_output, shell_output
    new_name = Engagement.find(engagement.id).name
    assert_equal "Eat a pop tart", new_name
  end

  def test_sad_path_editing_a_engagement
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
      pipe.puts "1" # Edit
      expected_output << "Enter a new name:\n"
      pipe.puts ""
      expected_output << "\"\" is not a valid engagement name.\n"
      expected_output << "Enter a new name:\n"
      pipe.puts "Eat a pop tart"
      expected_output << "Engagement has been updated to: \"Eat a pop tart\"\n"
      expected_output << main_menu
      pipe.puts "3" # Exit
      expected_output << "Program quit\n"
      shell_output = pipe.read
      pipe.close_write
      pipe.close_read
    end
    assert_equal expected_output, shell_output
    new_name = Engagement.find(engagement.id).name
    assert_equal "Eat a pop tart", new_name
  end

end