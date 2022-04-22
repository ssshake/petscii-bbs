FROM maven as build
WORKDIR /bbs
COPY . .
RUN mvn clean install package

FROM openjdk
EXPOSE 6510
WORKDIR /bbs
RUN chown -R nobody:nobody /bbs
COPY --from=build /bbs/target/* /bbs/
RUN chown -R nobody:nobody /bbs
user nobody
ENTRYPOINT ["java", "-jar","petscii-bbs.jar","--bbs","StdChoice:6510"]
