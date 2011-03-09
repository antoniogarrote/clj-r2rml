# This file is auto-generated from the current state of the database. Instead
# of editing this file, please use the migrations feature of Active Record to
# incrementally modify your database, and then regenerate this schema definition.
#
# Note that this schema.rb definition is the authoritative source for your
# database schema. If you need to create the application database on another
# system, you should be using db:schema:load, not running all the migrations
# from scratch. The latter is a flawed and unsustainable approach (the more migrations
# you'll amass, the slower it'll run and the greater likelihood for issues).
#
# It's strongly recommended to check this file into your version control system.

ActiveRecord::Schema.define(:version => 20110306010818) do

  create_table "candidates", :force => true do |t|
    t.string   "name"
    t.string   "surname"
    t.string   "birthdate"
    t.string   "nationality"
    t.string   "telephone"
    t.text     "address"
    t.string   "uri"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "educations", :force => true do |t|
    t.string   "start"
    t.string   "end"
    t.string   "institution"
    t.string   "titulation"
    t.string   "uri"
    t.string   "candidate"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "organizations", :force => true do |t|
    t.string   "name"
    t.string   "homepage"
    t.string   "uri"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

end
