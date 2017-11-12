require 'rubygems'
require 'bundler/setup'
require "minitest/reporters"
Dir["./app/**/*.rb"].each { |f| require f }
Dir["./lib/*.rb"].each { |f| require f }
ENV['TEST'] = "true"

reporter_options = { color: true }
Minitest::Reporters.use! [Minitest::Reporters::DefaultReporter.new(reporter_options)]

require 'minitest/autorun'
class Minitest::Test
  def setup
    Database.load_structure
    Database.execute("DELETE FROM engagements;")
  end
end

def log_engagements(name)
  Database.execute("INSERT INTO engagements (name) VALUES (?)", name)
end

def main_menu
  "1. Add a new engagement to your calendar\n2. List all upcoming engagements\n3. Exit\n"
end

def actions_menu
  "Would you like to?\n1. Edit\n2. Delete\n3. Exit\n"
end

def exit_from(pipe)
  pipe.puts "Exit"
  pipe.puts "3"
  "Exit\n" + main_menu + "Program quit\n"
end