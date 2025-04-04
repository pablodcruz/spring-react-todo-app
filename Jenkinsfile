pipeline {
    agent any

    environment {
        IMAGE_NAME = "spring-todo-app"
        CONTAINER_NAME = "spring-todo-container"
    }

    stages {
        stage('Clone Frontend Repo from main') {
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

        stage('Build Spring Boot JAR') {
            steps {
                dir('backend') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                dir('backend') {
                    sh 'docker build -t $IMAGE_NAME .'
                }
            }
        }

        stage('Deploy Container') {
            steps {
                sh '''
                docker stop $CONTAINER_NAME || true
                docker rm $CONTAINER_NAME || true
                docker run -d -p 8080:8080 --name $CONTAINER_NAME $IMAGE_NAME
                '''
            }
        }
        stage('Done') {
            steps {
                echo "✅ App deployed successfully at http://107.23.178.208:8080"
            }
        }

    }
}
