class EngagementController

  def index
    if Engagement.count > 0
      engagements = Engagement.all # All of the engagements in an array
      choose do |menu|
        menu.prompt = ""
        engagements.each do |engagement|
          menu.choice(engagement.name){ action_menu(engagement) }
        end
        menu.choice("Exit")
      end
    else
      "No engagements found. Add an engagement.\n"
    end
  end

  def action_menu(engagement)
    say("Would you like to?")
    choose do |menu|
      menu.prompt = ""
      menu.choice("Edit") do
        edit(engagement)
      end
      menu.choice("Delete") do
        delete(engagement)
      end
      menu.choice("Exit") do
        exit
      end
    end
  end

  def delete(engagement)
    Engagement.destroy(engagement.id)
    "#{engagement.name} has been removed"
  end


  def edit(engagement)
    loop do
      user_input = ask("Enter a new name:")
      engagement.name = user_input.strip
      if engagement.save
        say("Engagement has been updated to: \"#{engagement.name}\"")
        return
      else
        say(engagement.errors)
      end
    end
  end

  def add(name)
    name_cleaned = name.strip
    engagement = Engagement.new(name_cleaned)
    if engagement.save
      "\"#{name}\" has been added\n"
    else
      engagement.errors
    end
  end
end