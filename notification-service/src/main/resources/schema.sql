create table if not exists device
(
    device_id varchar primary key not null,
    token     varchar             null
);

create unique index if not exists idx_device_device_id on device (device_id);