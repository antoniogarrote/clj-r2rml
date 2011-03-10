class Jobs < ActiveRecord::Migration
  def self.up
    create_table :jobs do |t|
      t.string :uri
      t.string :position
      t.string :company
      t.string :description
      t.string :start_date
      t.string :end_date
      t.string :candidate
    end

    add_column :candidates, :email, :string
    add_column :educations, :description, :string
  end

  def self.down
    drop_table :jobs
    remove_column :candidates, :email
    remove_column :educations, :description
  end
end
