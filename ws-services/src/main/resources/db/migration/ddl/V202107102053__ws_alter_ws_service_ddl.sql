ALTER TABLE eg_ws_service ADD COLUMN IF NOT EXISTS deactivationDate bigint;
ALTER TABLE eg_ws_service_audit ADD COLUMN IF NOT EXISTS deactivationDate bigint;
