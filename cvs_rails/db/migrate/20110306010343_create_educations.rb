class CreateEducations < ActiveRecord::Migration
  def self.up
    create_table :educations do |t|
      t.string :start
      t.string :end
      t.string :institution
      t.string :titulation
      t.string :uri
      t.string :candidate

      t.timestamps
    end
  end

  def self.down
    drop_table :educations
  end
end
