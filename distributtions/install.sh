apt-get update
apt-get install -y \
            ca-certificates \
            curl \
            gnupg \
            lsb-release
mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
apt-get update
apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin docker-compose sshpass postgresql=12+214ubuntu0.1 golang

systemctl start postgresql

echo "SELECT 'CREATE DATABASE bdc_registration' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'bdc_registration')\gexec" | sudo -u postgres psql
echo "SELECT 'CREATE DATABASE bdc_runner' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'bdc_runner')\gexec" | sudo -u postgres psql
echo "SELECT 'CREATE DATABASE bdc_settings' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'bdc_settings')\gexec" | sudo -u postgres psql
cat schema_registration.sql | sudo -u postgres psql bdc_registration
cat schema_runner.sql | sudo -u postgres psql bdc_runner
cat schema_settings.sql | sudo -u postgres psql bdc_settings
cat data_settings.sql | sudo -u postgres psql bdc_settings

echo "ALTER USER postgres WITH PASSWORD 'postgres'" | sudo -u postgres psql

cp pg_hba.conf /etc/postgresql/12/main

systemctl restart postgresql
systemctl enable postgresql

mkdir /var/lib/bdc

cp docker-compose.yml /var/lib/bdc

cd go

go build
cp bdc-system-executor /usr/bin

cd /var/lib/bdc

systemctl start docker
systemctl enable docker

docker-compose pull
docker pull git.jinr.ru:5005/nica/docker-images/bmn:latest