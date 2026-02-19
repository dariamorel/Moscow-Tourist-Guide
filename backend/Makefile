IMAGE=tour-guide-tests

.PHONY: test build-image

build-image:
	docker build -f Dockerfile -t $(IMAGE) .

test: build-image
	docker run --rm \
	--network=host \
	-v /var/run/docker.sock:/var/run/docker.sock \
	$(IMAGE) \
	make test-local

test-local:
	cmake -S . -B build-debug
	cmake --build build-debug
	pytest -v