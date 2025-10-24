CREATE TABLE IF NOT EXISTS datasets (
                                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                                        name TEXT NOT NULL,
                                        created_at_ms INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS data_points (
                                           id INTEGER PRIMARY KEY AUTOINCREMENT,
                                           dataset_id INTEGER NOT NULL,
                                           x REAL NOT NULL,
                                           y REAL NOT NULL,
                                           FOREIGN KEY (dataset_id) REFERENCES datasets(id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_data_points_dataset_id ON data_points(dataset_id);