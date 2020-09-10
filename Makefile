
all: build test

prepare-build: update-jenkins update-plugins update-pipelines

build: prepare-build build-oraclelinux

update-plugins: 
	./update-plugins.sh < plugins.txt

update-jenkins: 
	./update-jenkins.sh 

update-pipelines: 
	./update-pipelines.sh 

build-oraclelinux:
	docker build --file Dockerfile -t jiab:0.1 .

bats:
	# Latest tag is unfortunately 0.4.0 which is quite older than the latest master tip.
	# So we clone and reset to this well known current sha:
	git clone https://github.com/sstephenson/bats.git ; \
	cd bats; \
	git reset --hard 03608115df2071fff4eaaff1605768c275e5f81f

prepare-test: bats
	git submodule update --init --recursive

test-oraclelinux: prepare-test
	DOCKERFILE=Dockerfile bats/bin/bats tests 

test: test-oraclelinux

test-install-plugins: prepare-test
	DOCKERFILE=Dockerfile-alpine bats/bin/bats tests/install-plugins.bats

publish:
	./publish.sh ; \

publish-experimental:
	./publish-experimental.sh ; \

clean:
	rm -rf tests/test_helper/bats-*; \
	rm -rf bats ; \
	# rm -rf plugins/* ; \
	rm -rf jenkins/*.war ; \
	# rm -rf pipelines/* ; \
	docker rmi jiab:0.1

