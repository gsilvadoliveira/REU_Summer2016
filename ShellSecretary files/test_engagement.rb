require_relative '../test_helper'

describe Engagement do
  describe "#all" do
    describe "if there are no engagements in the database" do
      it "should return an empty array" do
        assert_equal [], Engagement.all
      end
    end
    describe "if there are engagements" do
      before do
        log_engagements("09/09/2015 Happy Birthday")
        log_engagements("05/25/2015 Hair app @ Parlor & Juke")
        log_engagements("06/26/2015 Schools out for the summer!")
      end
      it "should return the engagements" do
        expected = ["09/09/2015 Happy Birthday", "05/25/2015 Hair app @ Parlor & Juke",
                    "06/26/2015 Schools out for the summer!"]
        actual = Engagement.all.map{ |engagement| engagement.name }
        assert_equal expected, actual
      end
    end
  end

  describe "#count" do
    describe "if there are no engagements in the database" do
      it "should return 0" do
        assert_equal 0, Engagement.count
      end
    end
    describe "if there are engagements" do
      before do
        log_engagements("09/09/2015 Happy Birthday")
        log_engagements("05/25/2015 Hair app @ Parlor & Juke")
        log_engagements("06/26/2015 Schools out for the summer!")
      end
      it "should return the correct count" do
        assert_equal 3, Engagement.count
      end
    end
  end

  describe ".initialize" do
    it "sets the name attribute" do
      engagement = Engagement.new("foo")
      assert_equal "foo", engagement.name
    end
  end

  describe ".save" do
    describe "if the model is valid" do
      let(:engagement){ Engagement.new("roast a pig") }
      it "should return true" do
        assert engagement.save
      end
      it "should save the model to the database" do
        engagement.save
        assert_equal 1, Engagement.count
        last_row = Database.execute("SELECT * FROM engagements")[0]
        database_name = last_row['name']
        assert_equal "roast a pig", database_name
      end
      it "should populate the model with id from the database" do
        engagement.save
        last_row = Database.execute("SELECT * FROM engagements")[0]
        database_id = last_row['id']
        assert_equal database_id, engagement.id
      end
    end

    describe "if the model is invalid" do
      let(:engagement){ Engagement.new("") }
      it "should return false" do
        refute engagement.save
      end
      it "should not save the model to the database" do
        engagement.save
        assert_equal 0, Engagement.count
      end
      it "should populate the error messages" do
        engagement.save
        assert_equal "\"\" is not a valid engagement name.", engagement.errors
      end
    end
  end

   describe ".valid?" do
    describe "with valid data" do
      let(:engagement){ Engagement.new("eat corn on the cob") }
      it "returns true" do
        assert engagement.valid?
      end
      it "should set errors to nil" do
        engagement.valid?
        assert engagement.errors.nil?
      end
    end
    describe "with no name" do
      let(:engagement){ Engagement.new(nil) }
      it "returns false" do
        refute engagement.valid?
      end
      it "sets the error message" do
        engagement.valid?
        assert_equal "\"\" is not a valid engagement name.", engagement.errors
      end
    end
    describe "with empty name" do
      let(:engagement){ Engagement.new("") }
      it "returns false" do
        refute engagement.valid?
      end
      it "sets the error message" do
        engagement.valid?
        assert_equal "\"\" is not a valid engagement name.", engagement.errors
      end
    end
    describe "with a name with no letter characters" do
      let(:engagement){ Engagement.new("777") }
      it "returns false" do
        refute engagement.valid?
      end
      it "sets the error message" do
        engagement.valid?
        assert_equal "\"777\" is not a valid engagement name.", engagement.errors
      end
    end
    describe "with a previously invalid name" do
      let(:engagement){ Engagement.new("666") }
      before do
        refute engagement.valid?
        engagement.name = "Eat a pop tart"
        assert_equal "Eat a pop tart", engagement.name
      end
      it "should return true" do
        assert engagement.valid?
      end
      it "should not have an error message" do
        engagement.valid?
        assert_nil engagement.errors
      end
    end

    describe "updating data" do
      describe "edit previously entered engagement" do
        let(:engagement_name){ "Eat a pop tart" }
        let(:new_engagement_name){ "Eat a toaster strudel" }
        it "should update engagement name but not id" do
          engagement = Engagement.new(engagement_name)
          engagement.save
          assert_equal 1, Engagement.count
          engagement.name = new_engagement_name
          assert engagement.save
          assert_equal 1, Engagement.count
          last_row = Database.execute("SELECT * FROM engagements")[0]
          assert_equal new_engagement_name, last_row['name']
        end
        it "shouldn't update other engagements' names" do
          bob = Engagement.new("Bob")
          bob.save
          engagement = Engagement.new(engagement_name)
          engagement.save
          assert_equal 2, Engagement.count
          engagement.name = new_engagement_name
          assert engagement.save
          assert_equal 2, Engagement.count

          bob2 = Engagement.find(bob.id)
          assert_equal bob.name, bob2.name
        end
      end
      describe "failed edit of previously entered engagement" do
        let(:engagement_name){ "Eat a pop tart" }
        let(:new_engagement_name){ "" }
        it "does not update anything" do
          engagement = Engagement.new(engagement_name)
          engagement.save
          assert_equal 1, Engagement.count
          engagement.name = new_engagement_name
          refute engagement.save
          assert_equal 1, Engagement.count
          last_row = Database.execute("SELECT * FROM engagements")[0]
          assert_equal engagement_name, last_row['name']
        end
      end
    end

    describe ".delete" do
      describe "delete previously added item" do
        it "should remove engagment from database" do
          engagement = Engagement.new("bob")
          engagement.save
          Engagement.destroy(engagement.id)
          table = Database.execute("SELECT * FROM engagements")
          assert_equal nil, table[0]
          assert_equal true, table.empty?
        end
      end
    end
  end
end


























