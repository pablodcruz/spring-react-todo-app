/*
   Clones both frontend and backend
   Builds the React app
   Copies the frontend into the Spring Boot static folder
   Since I gitignore application.properties. I need to inject application.properties from Jenkins credentials as a secret file.
   Builds the Spring Boot JAR
   Copies everything needed (including Dockerfile and backend code) to the EC2 instance
   Remotely builds the Docker image and runs the container
*/
pipeline {
    agent any

    environment {
        IMAGE_NAME = "spring-todo-app"
        CONTAINER_NAME = "spring-todo-container"
        EC2_HOST = "ec2-user@18.205.17.221"
    }

    stages {
        stage('Clone Frontend Repo') {
            steps {
                dir('frontend') {
                    git url: 'https://github.com/pablodcruz/react-todo-app.git', branch: 'main'
                }
            }
        }

        stage('Clone Backend Repo') {
            steps {
                dir('backend') {
                    git url: 'https://github.com/pablodcruz/spring-react-todo-app.git', branch: 'master'
                }
            }
        }

        stage('Build React App') {
            steps {
                dir('frontend') {
                    sh 'npm install'
                    sh 'npm run build'
                }
            }
        }

        stage('Copy Frontend into Spring Static Folder') {
            steps {
                sh 'rm -rf backend/src/main/resources/static/*'
                sh 'cp -r frontend/dist/* backend/src/main/resources/static/'
            }
        }

        stage('Inject application.properties') {
            steps {
                // Fix permissions in case directory is owned by root
                sh 'chmod -R u+w backend/src/main/resources/ || true'

                withCredentials([file(credentialsId: 'spring-boot-config', variable: 'CONFIG_FILE')]) {
                    sh 'cp $CONFIG_FILE backend/src/main/resources/application.properties'
                }
            }
        }

        stage('Build Spring Boot JAR') {
            steps {
                dir('backend') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Copy Project to EC2') {
            steps {
                withCredentials([file(credentialsId: 'ec2-ssh-key', variable: 'EC2_KEY')]) {
                    sh '''
                        chmod 400 $EC2_KEY
                        ssh -o StrictHostKeyChecking=no -i $EC2_KEY ec2-user@18.205.17.221 "mkdir -p spring-app"
                        scp -o StrictHostKeyChecking=no -i $EC2_KEY backend/Dockerfile ec2-user@18.205.17.221:/home/ec2-user/spring-app/
                        scp -o StrictHostKeyChecking=no -i $EC2_KEY backend/target/SpringTodoApp-0.0.1-SNAPSHOT.jar ec2-user@18.205.17.221:/home/ec2-user/spring-app/
                    '''
                }
            }
        }

        stage('Deploy Docker Container on EC2') {
            steps {
                withCredentials([file(credentialsId: 'ec2-ssh-key', variable: 'EC2_KEY')]) {
                    sh '''#!/bin/bash
ssh -o StrictHostKeyChecking=no -i $EC2_KEY ec2-user@18.205.17.221 <<EOF
    echo "🔎 Checking for process using port 8080..."
    PID=\$(lsof -t -i:8080 2>/dev/null)
    if [ ! -z "\$PID" ]; then
        echo "🔫 Killing process using port 8080 (PID: \$PID)"
        kill -9 \$PID
    fi

    echo "📦 Deploying new container..."
    cd spring-app
    docker stop spring-todo-container || true
    docker rm spring-todo-container || true
    docker build -t spring-todo-app .
    docker run -d -p 8080:8080 --name spring-todo-container spring-todo-app
EOF
'''
                }
            }
        }

        stage('Done') {
            steps {
                echo "✅ App deployed successfully at http://18.205.17.221:8080"
            }
        }
    }
}
