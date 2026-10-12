// swift-tools-version: 5.9
import PackageDescription

let package = Package(
  name: "gantree-ios",
  platforms: [
    .iOS(.v17),
    .macOS(.v13),
  ],
  products: [
    .library(name: "Mailbox", targets: ["Mailbox"])
  ],
  targets: [
    .target(name: "Mailbox"),
    .testTarget(
      name: "MailboxTests",
      dependencies: ["Mailbox"]
    ),
  ]
)
