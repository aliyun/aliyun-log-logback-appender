.PHONY: deploy
deploy:
	mvn clean deploy -Dmaven.test.skip=true

.PHONY: release
release: deploy
	mvn -Darguments="-DskipTests" release:clean release:prepare release:perform





