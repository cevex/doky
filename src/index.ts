#!/usr/bin/env node
import yargs, {Argv} from "yargs";

// Specify parameters
const options = yargs
    .usage("Usage: -n <name>")
    .option("n", { alias: "name", describe: "Your name", type: "string", demandOption: true })
    .argv;

const greeting = `Hello, ${options.name}!`;
console.log(greeting);

