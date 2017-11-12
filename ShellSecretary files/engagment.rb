class Engagement
  attr_accessor :name
  attr_reader :id, :errors

  def initialize(name = nil)
    self.name = name
  end

  def self.populate_from_database(row)
    engagement = Engagement.new
    engagement.name = row['name']
    engagement.instance_variable_set(:@id, row['id'])
    engagement
  end

  def self.destroy(id)
    Database.execute("DELETE FROM engagements WHERE id = ?", id)
    @id = nil
  end

  def self.all
    Database.execute("SELECT * FROM engagements ORDER BY id").map do |row|
      populate_from_database(row)
    end
  end

  def self.find(id)
    row = Database.execute("SELECT * FROM engagements WHERE id = ?", id).first
    row ? populate_from_database(row) : nil
  end

  def save
    return false unless valid?
    if @id
      Database.execute("UPDATE engagements SET name=? WHERE id=?", name, id)
      true
    else
      Database.execute("INSERT INTO engagements (name) VALUES (?)", name)
      @id = Database.execute("SELECT last_insert_rowid()")[0]['last_insert_rowid()']
      true
    end
  end

  def valid?
    if name.nil? or name.empty? or /^\d+$/.match(name)
      @errors = "\"#{name}\" is not a valid engagement name."
      false
    else
      @errors = nil
      true
    end
  end

  def self.count
    Database.execute("select count(id) from engagements")[0][0]
  end

end