echo "compiling project..."
cd node
mvn clean package
cd ../backend
mvn clean package
cd ../frontend
npm install

if [ $? -ne 0 ]; then
    echo "Build failed"
    exit 1
fi
