pipeline {
    agent any

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
    }

    environment {
        APP_NAME = 'sb-concepts-lab'
        APP_IMAGE = "sb-concepts-lab:${BUILD_NUMBER}"
        APP_CONTAINER = "sb-concepts-lab-ci-${BUILD_NUMBER}"
        APP_PORT = '18080'
        SPRING_PROFILES_ACTIVE = 'local'
        SERVER_PORT = '8080'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Test') {
            steps {
                sh './mvnw -B clean test'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                sh './mvnw -B -DskipTests package'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'docker build --pull -t "$APP_IMAGE" .'
            }
        }

        stage('Smoke Test Docker Image') {
            steps {
                sh '''
                    set -eu
                    docker rm -f "$APP_CONTAINER" >/dev/null 2>&1 || true
                    docker run -d \
                      --name "$APP_CONTAINER" \
                      -e SPRING_PROFILES_ACTIVE="$SPRING_PROFILES_ACTIVE" \
                      -e SERVER_PORT="$SERVER_PORT" \
                      -p "$APP_PORT:8080" \
                      "$APP_IMAGE"

                    for i in $(seq 1 30); do
                      if curl -fsS "http://localhost:$APP_PORT/actuator/health" >/dev/null; then
                        break
                      fi
                      if [ "$i" -eq 30 ]; then
                        docker logs "$APP_CONTAINER"
                        exit 1
                      fi
                      sleep 2
                    done

                    curl -fsS "http://localhost:$APP_PORT/app/v1/test/message" | grep -q '^test$'
                '''
            }
            post {
                always {
                    sh 'docker logs "$APP_CONTAINER" || true'
                    sh 'docker rm -f "$APP_CONTAINER" >/dev/null 2>&1 || true'
                }
            }
        }
    }

    post {
        always {
            sh 'docker rm -f "$APP_CONTAINER" >/dev/null 2>&1 || true'
        }
        success {
            echo "CI/CD pipeline completed successfully for ${APP_NAME}."
        }
    }
}
