--liquibase formatted sql
--changeset satoshi:202504191559
--comment: set unblock_reason nullable
 
ALTER TABLE BLOCKS MODIFY COLUMN unblock_reason VARCHAR(255) NULL;
 
 --rollback ALTER TABLE BLOCKS MODIFY COLUMN unblock_reason VARCHAR(255) NOT NULL;