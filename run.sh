echo "executing project..."
cd backend
java -jar target/backend-0.0.1-SNAPSHOT.jar

if [ $? -ne 0 ]; then
    echo "Build failed"
    exit 1
fi
