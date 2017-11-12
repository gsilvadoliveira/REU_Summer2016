require_relative "../test_helper"

describe EngagementController do

  describe ".index" do
    let(:controller) {EngagementController.new}

    it "should say no engagements found when empty" do
      actual_output = controller.index
      expected_output = "No engagements found. Add an engagement.\n"
      assert_equal expected_output, actual_output
    end
  end

  describe ".add" do
    let(:controller) {EngagementController.new}

    it "should add an engagement" do
      controller.add("run with scissors")
      assert_equal 1, Engagement.count
    end

    it "should not add scenario all spaces" do
      engagement_name = "       "
      assert_equal "\"\" is not a valid engagement name.",  controller.add(engagement_name)
    end

    it "should only add scenarios that make sense" do
      engagement_name = "77777777"
      controller.add(engagement_name)
      assert_equal 0, Engagement.count
    end

  end

end