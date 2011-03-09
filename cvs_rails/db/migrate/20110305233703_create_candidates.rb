class CreateCandidates < ActiveRecord::Migration
  def self.up
    create_table :candidates do |t|
      t.string :name
      t.string :surname
      t.string :birthdate
      t.string :nationality
      t.string :telephone
      t.text :address
      t.string :uri

      t.timestamps
    end
  end

  def self.down
    drop_table :candidates
  end
end
