if [ ! -d /tmp/bdc ]; then
    mkdir /tmp/bdc
fi

cd /var/lib/bdc

docker-compose up -d

bdc-system-executor


