#!/usr/bin/env ruby
require 'highline/import'

Dir["./app/**/*.rb"].each { |f| require f }
Dir["./lib/*.rb"].each { |f| require f }


if ARGV.size == 1 and ARGV[0] == "manage"
  loop do
    choose do |menu|
      menu.prompt = ""

      engagement_controller = EngagementController.new
      menu.choice('Add a new engagement to your calendar') do
        loop do
          user_input = ask("what engagement would you like to add?")
          response = engagement_controller.add(user_input)
          say(response) unless response.nil?
          if /has\sbeen\sadded$/.match(response)
            break
          end
        end
      end
      menu.choice('List all upcoming engagements') do
        say(engagement_controller.index)
      end
      menu.choice('Exit') do
        say("Program quit\n")
        exit
      end
    end
  end
else
   print  "[Help] Run as: ./shell_secretary [manage]\n"
end
