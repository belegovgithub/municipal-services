ALTER TABLE eg_ws_service ADD COLUMN IF NOT EXISTS lastmeterreading numeric(12,3);
ALTER TABLE eg_ws_service_audit ADD COLUMN IF NOT EXISTS lastmeterreading numeric(12,3);
