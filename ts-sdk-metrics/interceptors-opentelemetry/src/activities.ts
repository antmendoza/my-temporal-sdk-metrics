import {ApplicationFailure} from "@temporalio/activity";

export async function greet(name: string): Promise<string> {


  await new Promise(r => setTimeout(r, 1_000));

  return `Hello, ${name}!`;
}

export async function greet_failed(name: string): Promise<string> {

 throw ApplicationFailure.nonRetryable("", "");

}


export async function greetLocal(name: string): Promise<string> {
  return `Hello, ${name}!`;
}
