class AddMakerToCandidates < ActiveRecord::Migration
  def self.up
    create_table :acls do |t|
      t.string :maker
      t.string :graph
      t.string :read
      t.string :write
      t.string :rdftype
    end

  end

  def self.down
    drop_table :acls
  end
end
