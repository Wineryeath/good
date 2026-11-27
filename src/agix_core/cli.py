"""Command-line interface for the initial AGI-X core."""

import argparse


def main() -> None:
    parser = argparse.ArgumentParser(
        prog="agix",
        description="AGI-X task orchestration core (initial scaffold).",
    )
    subparsers = parser.add_subparsers(dest="command")
    subparsers.add_parser("version", help="show the installed version")
    subparsers.add_parser("doctor", help="check the local Python runtime")
    args = parser.parse_args()

    if args.command == "version":
        from agix_core import __version__
        print(f"AGI-X {__version__}")
    elif args.command == "doctor":
        import sys
        print(f"Python: {sys.version.split()[0]}")
        print("Runtime check: OK")
        print("Model provider: not configured")
        print("Tool adapters: none registered")
    else:
        parser.print_help()


if __name__ == "__main__":
    main()
